package com.timelog.timelog.controller;

import com.google.common.collect.Lists;
import com.timelog.timelog.exceptions.CompanyNotFoundException;
import com.timelog.timelog.models.Company;
import com.timelog.timelog.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.timelog.timelog.constants.TimeLogConstants.COMPANIES_PATH;
import static com.timelog.timelog.constants.TimeLogConstants.TIME_LOG_V1_PATH;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(TIME_LOG_V1_PATH)
public class CompanyController {

    private CompanyRepository companyRepository;

    @Autowired
    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    @GetMapping(COMPANIES_PATH)
    public ResponseEntity<Map<String, Object>> getCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false/*, defaultValue = "0"*/) Integer page,
            @RequestParam(required = false/*, defaultValue = "3"*/) Integer size,
            @RequestParam(required = false, defaultValue = "id,asc") String[] sort) {

        try {
            List<Sort.Order> orders = new ArrayList<Sort.Order>();

            if (sort[0].contains(",")) {
                // will sort more than 2 fields
                // sortOrder="field, direction"
                for (String sortOrder : sort) {
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
                }
            } else {
                // sort=[field, direction]
                orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
            }

            Map<String, Object> responseAll = new HashMap<>();

            List<Company> companies;
            if (page == null) {
                companies = companyRepository.findAll(Sort.by(orders));
                responseAll.put("companies",companies);
                return new ResponseEntity<>(responseAll, HttpStatus.OK);
            }

            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<Company> pageTuts;
            if (name == null)
                pageTuts = companyRepository.findAll(pagingSort);
            else
                pageTuts = companyRepository.findByNameContaining(name, pagingSort);

            companies = pageTuts.getContent();

            if (companies.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            Map<String, Object> response = new HashMap<>();

            response.put("companies", companies);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalCompanies", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(COMPANIES_PATH + "/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable("id") String id) {
        Optional<Company> optionalResponse = companyRepository.findById(id);
        if (!optionalResponse.isPresent()) {

            throw new CompanyNotFoundException(id);
        }
        return new ResponseEntity<>(optionalResponse.get(), HttpStatus.OK);
    }


    @PostMapping(COMPANIES_PATH)
    public @ResponseBody
    ResponseEntity<Company> addCompany(@Validated @RequestBody Company company) {
        companyRepository.save(company);
        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @DeleteMapping(COMPANIES_PATH + "/{id}")
    public void deleteCompany(@PathVariable("id") String id) {

        Optional<Company> optionalResponse = companyRepository.findById(id);
        if (!optionalResponse.isPresent()) {

            throw new CompanyNotFoundException(id);
        }
        companyRepository.deleteById(id);
    }

    @PutMapping(COMPANIES_PATH + "/{id}")
    public ResponseEntity<Company> updateCompanyById(@Validated @RequestBody Company company, @PathVariable("id") String id) {

        Optional<Company> optionalResponse = companyRepository.findById(id);
        if (!optionalResponse.isPresent()) {

            throw new CompanyNotFoundException(id);
        }
        company.id = id;
        companyRepository.save(company);
        return new ResponseEntity<>(company, HttpStatus.OK);
    }

}
